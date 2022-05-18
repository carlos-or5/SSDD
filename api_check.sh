#! /bin/bash

# Bash script to check API

if [ $# -ne 2 ];
then
  echo "Usage ${0} USERNAME TOKEN"
  exit 1
fi

# 

URL="http://localhost:8080/rest/videos/upload"
FILE="/home/peter/Downloads/test.mp4"

datee=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

auth_token=$(echo -n "${URL}${datee}${2}" | md5sum | awk '{print $1}')

echo "Request to: ${URL}"

response=$(/usr/bin/curl -sS -w "\n%{http_code}" -H "User: ${1}" -H "Date: ${datee}" -H "Auth-Token: ${auth_token}" -F "file=@${FILE}" -X POST "${URL}")
response_body=$(echo "${response}" | head -n-1)
http_code=$(echo "${response}" | tail -n1)

if [ ${http_code} -ne 202 ];
then
  echo "HTTP Error Uploading: ${http_code}"
  exit 2
fi

echo "HTTP: ${http_code}"

location=$(echo "${response_body}" | jq '.["location"]' | tr -d '"')

url_video="http://localhost:8080/rest/videos/${location}"

auth_token_2=$(echo -n "${url_video}${datee}${2}" | md5sum | awk '{print $1}')

echo "Request to: ${url_video}"

response=$(/usr/bin/curl -sS -w "\n%{http_code}" -H "User: ${1}" -H "Date: ${datee}" -H "Auth-Token: ${auth_token_2}" "${url_video}")
response_body=$(echo "${response}" | head -n-1)
http_code=$(echo "${response}" | tail -n1)

if [ ${http_code} -ne 204 -a ${http_code} -ne 200 ];
then
  echo "HTTP Error Getting Video: ${http_code}"
  exit 3
fi

while [ ${http_code} -eq 204 ];
do
  response_body=$(/usr/bin/curl -sS -w "\n%{http_code}" -H "User: ${1}" -H "Date: ${datee}" -H "Auth-Token: ${auth_token_2}" "${url_video}")
  http_code=$(echo "${response_body}" | tail -n1)
  echo "Not processed yet :("
  echo "HTTP: ${http_code}"
  sleep 1
done

echo "${response_body}"
