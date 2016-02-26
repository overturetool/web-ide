#!/usr/bin/env bash

# -m 128M seems to be the minimum

docker run -d \
    -p 9000:9000 \
    -m 128M \
    --memory-swap -1 \
    --env "JAVA_OPTS=-Xms64m -Xmx128m -Xss1M -XX:+CMSClassUnloadingEnabled" \
    --ulimit nofile=1024 \
    --name overture_api kdsaaby/overture_api