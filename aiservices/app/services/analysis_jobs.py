from asyncio import Lock
from dataclasses import dataclass, field
from datetime import datetime
from queue import Queue
from threading import Thread
from typing import Any, Optional
from uuid import uuid4


@dataclass
class AnalysisJob:
    job_id: str
    question: str
    status: str
    progress: int =0
    response: dict[str, Any] |None  = None
    error: str |None = None
    created_at: datetime = field(default_factory=lambda: datetime.now())
    updated_at: datetime = field(default_factory=lambda: datetime.now())


__jobs: dict[str, AnalysisJob] = {}
__jobs_lock = Lock()  # Placeholder for a threading.Lock() or asyncio.Lock() if needed
__job_queue: Queue[str] = Queue()  # Placeholder for a queue to manage job processing
worker_started = False  # Flag to indicate if the worker has started
worker_lock = Lock()  # Lock to ensure only one worker is started

def submit_analyze_job(question: str) -> dict[str, Any]:
    _start_worker_once()
    job = AnalysisJob(
        job_id=str(uuid4()),
        question=question)

    with __jobs_lock:
        __jobs[job.job_id] = job
    __job_queue.put(job.job_id)
    return __job_to_dict(job)

def get_analysis_job(job_id: str) -> dict[str, Any] | None:
    with __jobs_lock:
        job = __jobs.get(job_id)
        if job is None:
            return None
        return __job_to_dict(job)

def _start_worker_once():
    global _worker_started
    with worker_lock:
        if _worker_started:
            return

        worker = Thread(
            target = _worker_loop,
            name = "analysis-job-worker",
            daemon = True
        )

        worker.start()
        _worker_started = True

def _worker_loop() -> None:
    while True:
        job_id = __job_queue.get()
        if job_id is None:
            break
        try:
            _run_job(job_id)
        finally:
            __job_queue.task_done()

def _run_job(job_id: str) -> None:
    job = _update_job(job_id, status="in_progress", progress=0)
    if job is None:
        return
    try:
        _update_job(job_id, progress=50)
        # Simulate some processing time
        response = run_analysis_workflow(job.question)
        _update_job(job_id, status="completed", progress=100, response=response, error=None)
    except Exception as e:
        _update_job(job_id, status="completed", progress=100, response=None, error=str(e))

def _update_job(job_id: str, **updates: Any) -> AnalysisJob | None:
    with __jobs_lock:
        job = __jobs.get(job_id)
        if job is None:
            return None
        for key, value in updates.items():
            if hasattr(job, key):
                setattr(job, key, value)
        job.updated_at = datetime.now()
        return job

def __job_to_dict(job: AnalysisJob) -> dict[str, Any]:
    return {
        "job_id": job.job_id,
        "question": job.question,
        "status": job.status,
        "progress": job.progress,
        "response": job.response,
        "error": job.error,
        "created_at": job.created_at.isoformat(),
        "updated_at": job.updated_at.isoformat()
    }