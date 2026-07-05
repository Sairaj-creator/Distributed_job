# TaskFlow API

## CLI

| Command | Description |
|---|---|
| `list-workflows` | Prints registered workflow ids and names. |
| `show-workflow <id>` | Prints details for one workflow. |
| `trigger <workflowId>` | Triggers a workflow immediately. |
| `pause <workflowId>` | Pauses a workflow. |
| `resume <workflowId>` | Resumes a workflow. |
| `history <jobId> [--limit N]` | Shows recent runs for one job. |
| `stats` | Prints aggregate run statistics. |

## HTTP

| Endpoint | Description |
|---|---|
| `GET /status` | JSON summary of registered workflows and metrics. |
| `GET /workflows/{id}` | JSON detail for one workflow. |
