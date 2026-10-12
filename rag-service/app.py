"""FastAPI sidecar that serves an embedchain RAG bot over HTTP.

Any client (including the Java chat app) can POST /query to get an answer
grounded in the documents under RAG_DOCS_DIR.

Run it with:

    uvicorn app:app --host 0.0.0.0 --port 8000
"""

from __future__ import annotations

import logging
import os
from contextlib import asynccontextmanager
from pathlib import Path
from typing import Any

from dotenv import load_dotenv
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

load_dotenv()

from embedchain import App  # noqa: E402  (must import after env vars are loaded)

logging.basicConfig(level=os.getenv("LOG_LEVEL", "INFO"))
logger = logging.getLogger("rag-service")

DOCS_DIR = Path(os.getenv("RAG_DOCS_DIR", Path(__file__).resolve().parent / "docs"))
SUPPORTED_SUFFIXES = {".pdf", ".txt", ".md", ".mdx", ".docx", ".json", ".csv", ".xml"}


class QueryRequest(BaseModel):
    question: str = Field(..., min_length=1)
    citations: bool = False


class QueryResponse(BaseModel):
    answer: str
    citations: list[Any] = Field(default_factory=list)


class IngestResponse(BaseModel):
    loaded: list[str]
    skipped: list[str]


def _build_bot() -> App:
    config_path = os.getenv("RAG_CONFIG_PATH")
    if config_path:
        logger.info("Loading embedchain config from %s", config_path)
        return App.from_config(config_path=config_path)
    return App()


bot: App = _build_bot()
_loaded: set[str] = set()


def ingest_docs(force: bool = False) -> IngestResponse:
    """Embed every supported file under DOCS_DIR, skipping ones already loaded."""
    loaded: list[str] = []
    skipped: list[str] = []
    DOCS_DIR.mkdir(parents=True, exist_ok=True)
    for path in sorted(DOCS_DIR.rglob("*")):
        if not path.is_file() or path.suffix.lower() not in SUPPORTED_SUFFIXES:
            continue
        key = str(path.relative_to(DOCS_DIR))
        if not force and key in _loaded:
            skipped.append(key)
            continue
        logger.info("Embedding %s", path)
        bot.add(str(path))
        _loaded.add(key)
        loaded.append(key)
    return IngestResponse(loaded=loaded, skipped=skipped)


@asynccontextmanager
async def lifespan(_: FastAPI):
    try:
        result = ingest_docs()
        logger.info("Ingest complete: %d loaded, %d skipped", len(result.loaded), len(result.skipped))
    except Exception:  # noqa: BLE001 - keep the service up even if ingest fails
        logger.exception("Initial ingest failed; /query may be empty until /ingest succeeds")
    yield


app = FastAPI(title="Realtime Chat RAG sidecar", version="0.1.0", lifespan=lifespan)


@app.get("/health")
def health() -> dict[str, Any]:
    return {"status": "ok", "docs_dir": str(DOCS_DIR), "loaded": sorted(_loaded)}


@app.post("/ingest", response_model=IngestResponse)
def ingest(force: bool = False) -> IngestResponse:
    """Re-scan the docs folder. Pass ?force=true to re-embed already-loaded files."""
    return ingest_docs(force=force)


@app.post("/query", response_model=QueryResponse)
def query(request: QueryRequest) -> QueryResponse:
    question = request.question.strip()
    if not question:
        raise HTTPException(status_code=400, detail="question must not be blank")
    try:
        if request.citations:
            answer, citations = bot.query(question, citations=True)
            return QueryResponse(answer=str(answer), citations=list(citations))
        return QueryResponse(answer=str(bot.query(question)))
    except Exception as exc:  # noqa: BLE001
        logger.exception("Query failed")
        raise HTTPException(status_code=502, detail=str(exc)) from exc


if __name__ == "__main__":
    import uvicorn

    uvicorn.run("app:app", host=os.getenv("HOST", "0.0.0.0"), port=int(os.getenv("PORT", "8000")))
