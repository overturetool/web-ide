*This file will be packaged with your application, when using `activator dist`.*

## Hosts
* Heroku: https://boiling-mesa-1444.herokuapp.com
* Local: localhost:9000

## Endpoints
#### readFile
`
localhost:9000/vfs/<account>/<path>*/file.extension
`

#### readdir
`
localhost:9000/vfs/<account>/<path>*
`

#### debug
`
ws://localhost:9000/debug/kdsaaby/kds_ws/bom.vdmsl?entry=UGFydHMoMSwgYm9tKQ==
`

where:

`
Base64.decode(UGFydHMoMSwgYm9tKQ==) = "Parts(1, bom)"
`