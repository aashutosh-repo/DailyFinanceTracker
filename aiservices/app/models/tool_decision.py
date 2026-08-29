from typing import Optional, List

from pydantic import BaseModel, Field

from app.models.tool_call import ToolCall


class ToolDecision(BaseModel):

    tool_calls: List[ToolCall] = Field(
        default_factory=list
    )

    response: Optional[str] = None