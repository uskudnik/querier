FROM clojure
COPY . /querier
WORKDIR /querier
CMD ["bash"]