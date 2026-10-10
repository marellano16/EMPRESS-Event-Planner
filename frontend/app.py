import os

import streamlit as st

BACKEND_URL = os.environ.get("BACKEND_URL", "http://localhost:8080")

st.set_page_config(page_title="Empress Event Planner")
st.title("Empress Event Planner")
st.caption("Enter an event and a member count to get a materials list and budget.")

st.info(f"Placeholder page. Backend expected at {BACKEND_URL}/plan")
