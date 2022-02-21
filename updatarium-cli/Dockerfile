FROM openjdk:17-slim

ADD *.tar /
RUN ln -s /updatarium-cli*/ /updatarium-cli


WORKDIR /updatarium-cli
USER nobody

ENTRYPOINT ["/updatarium-cli/bin/updatarium-cli"]
CMD ["--help"]