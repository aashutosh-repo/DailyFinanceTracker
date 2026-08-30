from typing import Optional, Dict, Any, List

from pydantic import BaseModel, Field


class ToolCall(BaseModel):

    tool_name: str

    arguments: Dict[str, Any] = Field(
        default_factory=dict
    )


class ToolDecision(BaseModel):

    tool_calls: List[ToolCall] = Field(
        default_factory=list
    )

    response: Optional[str] = None