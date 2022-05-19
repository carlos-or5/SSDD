# Proyecto SSDD 2022. 
## Componentes Grupo:
- Carlos Ortiz Rodríguez
- Pedro Gabaldón Juliá

# Ejecucion

Gracias al CI/CD con Github Actions la ultima compilacion y construccion de las imagenes Docker exitosa está disponible en el repositorio **ssdd** del usuario **peterg11**. Este repositorio es privado, entregaremos una token en la tarea para poder hacer login mediante.

```bash
docker login
```

Por tanto para poner en marcha el entorno, una vez realizado el login, es suficiente con lanzar una de estas, por ejemplo, alternativas  tras haber descargado **docker-compose.yml**.

```bash
docker-compose up
docker-compose up -d
```

![alt text](https://i.imgur.com/3JGMVXr.png)

# Compilación

El proyecto se apoya en **Maven** y **Make** para gestionar las dependendencias y compilación.

En primer lugar, será necesario clonar el repositorio.

```bash
git clone https://github.com/carlos-or5/SSDD.git
```

Una vez clonado, lanzar **make**.

El proyecto comenzará a compilarse y una vez termine satisfactoriamente se puede utilizar el otro fichero alternativo para **docker-compose-devel.yml** para que construya las imagenes Docker desde los Dockerfile presentes en los correspondientes directorios.

# Prueba API

El script **api_check.sh** sirve para realizar un testing automatica de la API mediante **curl**. Recibe como parametros el nombre de usuario y la token de este. Por defecto trata de subir el video presente en **home/peter/Downloads/test.mp4**, esto será necesario modificarlo acorde. Existe ya en el repositorio una captura de red de ejemplo de la API, **api.pcapng**

```bash
./api_check.sh test supertoken
```

![alt text](https://i.imgur.com/PppuNnBh.jpg)
