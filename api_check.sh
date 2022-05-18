#! /bin/bash

# Bash script to check API

if [ $# -ne 2 ];
then
  echo "Usage ${0} USERNAME TOKEN"
  exit 1
fi

# 

URL="http://localhost:8080/rest/videos/upload"

datee=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

auth_token=$(echo -n "${URL}${datee}${2}" | md5sum)

response_body=$(/usr/bin/curl -sS -vvv -H "User: ${1}" -H "Date: ${datee}" -H "Auth-Token: ${auth_token}" -F "file=@/home/peter/Downloads/face-demographics-walking-and-pause.mp4" -X POST "${URL}")

location=$(echo "${response_body}" | jq '.["location"]')

url_video="http://localhost:8080/rest/videos/${location}"

auth_token_2=$(echo -n "${url_video}${datee}${2}" | md5sum)

http_code=$(/usr/bin/curl -sS -o -w "%{http_code}" -vvv -H "User: ${1}" -H "Date: ${datee}" -H "Auth-Token: ${auth_token}" -F "file=@/home/peter/Downloads/face-demographics-walking-and-pause.mp4" -X POST "${URL}" | tail -n1)

while [ http_code -eq 204 ];
do
  http_code=$(/usr/bin/curl -sS -o -w "%{http_code}" -vvv -H "User: ${1}" -H "Date: ${datee}" -H "Auth-Token: ${auth_token}" -F "file=@/home/peter/Downloads/face-demographics-walking-and-p    ause.mp4" -X POST "${URL}" | tail -n1)
done

echo "${http_code}"
