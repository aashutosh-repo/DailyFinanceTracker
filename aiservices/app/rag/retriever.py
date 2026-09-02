from app.rag.vector_store import get_vector_store, vector_store

class LazyRetriever:

    def invoke(self, question: str):
        return get_vector_store().as_retriever(
            search_type="similarity",
            search_kwargs={"k": 10}
            ).invoke(question)
    
retrieve = LazyRetriever().invoke