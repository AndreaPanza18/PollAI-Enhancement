FROM python:3.11-slim
WORKDIR /notebooks

RUN pip install --no-cache-dir numpy pandas scikit-learn joblib notebook
COPY . .

EXPOSE 8888
CMD ["jupyter", "notebook", "--ip=0.0.0.0", "--port=8888", "--no-browser", "--allow-root"]
