*This file will be packaged with your application, when using `activator dist`.*

## Hosts
* Local: localhost:9000

## Endpoints
#### readFile
`
localhost:9000/vfs/readFile/<account>/<path>*/file.extension
`

#### readdir
`
localhost:9000/vfs/readdir/<account>/<path>*
`

### move
`
localhost:9000/vfs/move/<account>/[<path>*|file.extension]
`

    {
       "destination": "test1/test_ws/1/2a",
       "collisionPolicy": "Stop"
    }

collisionPolicy = { Stop, Override, KeepBoth }


#### debug
`
ws://localhost:9000/debug/kdsaaby/kds_ws/bom.vdmsl?entry=UGFydHMoMSwgYm9tKQ==
`

where:

`
Base64.decode(UGFydHMoMSwgYm9tKQ==) = "Parts(1, bom)"
`

#### debug project

Give path to the base directory, AND specify type of project as query string parameter.

`
ws://localhost:9000/debug/kdsaaby/kds_ws/barSL?entry=VGVzdEJhZ0FsbCgp&type=vdmsl
`

### lint

`
localhost:9000/lint/kdsaaby/kds_ws/bom.vdmsl
`

### codecompletion
`
localhost:9000/codecompletion/kdsaaby/kds_ws/bom.vdmsl?line=20&column=1
`
