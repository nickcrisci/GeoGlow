FROM python:slim

WORKDIR /service

COPY requirements.txt .

RUN pip install -r requirements.txt

COPY . .

CMD ["python", "-u", "service.py"]