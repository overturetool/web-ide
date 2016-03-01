#!/usr/bin/env bash

echo " - cleanup"
docker stop overture_api
docker rm overture_api

echo " - compile app"
sbt clean compile stage

echo " - build images"
docker build -t overture/api .
echo " - build finished"

#echo " - run container"
#docker run -d -p 9000:9000 --name overture_api overture/api

#docker exec -it overture_api bash
#docker logs -f overture_api

docker tag overture/api kdsaaby/overture_api
docker push kdsaaby/overture_api
