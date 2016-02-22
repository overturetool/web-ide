#!/usr/bin/env bash

docker stop overture_api
docker rm overture_api
docker build -t overture/api .
#docker run -d -t -p 9000:9000 --name overture_api overture/api
#docker logs -f overture_api
docker tag -f overture/api kdsaaby/overture_api
docker push kdsaaby/overture_api