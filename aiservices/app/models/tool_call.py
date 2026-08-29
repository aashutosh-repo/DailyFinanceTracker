from typing import Dict, Any

from pydantic import BaseModel, Field


class ToolCall(BaseModel):

    tool_name: str

    arguments: Dict[str, Any] = Field(
        default_factory=dict
    )