

from app.services.analysis_workflow import run_analysis_workflow


def ask_assistant(question: str):
    return run_analysis_workflow(question)