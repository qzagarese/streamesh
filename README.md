Streamesh
=========
Streamesh is a Distributed Data Pipeline Orchestration Platform.
It allows for the deployment and execution of atomic and complex data services.
Atomic services are `Micropipes` and complex ones are `Flows`.

`Flows` can be an aggregation of `Micropipes` or further `Flows` or a mix of both.

Streamesh comes with a server, a shell and a web-ui component.

## Running Streamesh
You need the following to build and run Streamesh:
- Docker 19+
- Docker Compose 1.24.1+

Firstly, clone this repo.

`git clone git@github.com:scicast-io/streamesh.git`

Or

`git clone https://github.com/scicast-io/streamesh.git`

From inside the `streamesh` directory, run the following to build the server (this might take a minute or two):

`docker build -t streamesh-server --target streamesh-server .`

Once the image has been created, you can run the following the build the web UI (this could also take a minute):

`docker build -t streamesh-web-ui --target streamesh-web-ui .`

Finally, to run Streamesh, simply run this command:

`docker-compose up`