from app.rag.vector_store import vector_store


def reset():

    print("Deleting vector collection...")

    vector_store.delete_collection()

    print("Collection deleted.")


if __name__ == "__main__":

    reset()