# Realtime Chat — RAG sidecar

A tiny [FastAPI](https://fastapi.tiangolo.com/) service that wraps an
[embedchain](https://github.com/embedchain/embedchain) RAG bot. It embeds the
files in its `docs/` folder and answers questions over HTTP. The Java chat app
calls it to power the `/ask` command.

## Endpoints

| Method | Path             | Body                                        | Returns                          |
|--------|------------------|---------------------------------------------|----------------------------------|
| GET    | `/health`        | –                                           | Status + list of loaded files    |
| POST   | `/ingest?force=` | –                                           | `{ "loaded": [...], "skipped": [...] }` |
| POST   | `/query`         | `{ "question": "...", "citations": false }` | `{ "answer": "...", "citations": [] }` |

## Setup

Requires Python 3.9+.

```bash
cd rag-service
python -m venv .venv
# Windows
. .venv/Scripts/activate
# macOS / Linux
# source .venv/bin/activate

pip install -r requirements.txt
cp .env.example .env      # then set OPENAI_API_KEY
```

## Run

```bash
uvicorn app:app --host 0.0.0.0 --port 8000
```

On startup the service embeds every supported file in `docs/`. To add knowledge,
drop files into `docs/` and call `POST /ingest` (or restart).

```bash
curl -X POST "http://localhost:8000/query" \
  -H "Content-Type: application/json" \
  -d '{"question": "How do I run the chat app?"}'
```

## Configuration

Set via environment variables (or `.env`):

| Variable          | Default        | Purpose                                                    |
|-------------------|----------------|------------------------------------------------------------|
| `OPENAI_API_KEY`  | –              | Required by the default embedchain config (OpenAI models)  |
| `RAG_DOCS_DIR`    | `./docs`       | Folder to embed                                            |
| `RAG_CONFIG_PATH` | –              | Optional embedchain `config.yaml` to switch LLM/embedder   |
| `HOST` / `PORT`   | `0.0.0.0`/`8000` | Bind address / port                                      |
| `LOG_LEVEL`       | `INFO`         | Python logging level                                       |

To use a non-OpenAI provider (e.g. a local model), create a `config.yaml` and
point `RAG_CONFIG_PATH` at it. See the
[embedchain docs](https://docs.embedchain.ai/).

## Supported file types

`.pdf`, `.txt`, `.md`, `.mdx`, `.docx`, `.json`, `.csv`, `.xml`

## Docker

```bash
docker build -t realtime-chat-rag ./rag-service
docker run --rm -p 8000:8000 --env-file rag-service/.env realtime-chat-rag
```

## Using it from the chat app

In the Java app set:

```properties
rag.enabled=true
rag.base-url=http://localhost:8000
rag.command=/ask
rag.bot-name=Assistant
```

Then type `/ask how do I run the app?` in the chat; the bot's answer is
broadcast to everyone in the room.
