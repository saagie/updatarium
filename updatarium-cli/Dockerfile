FROM openjdk:11

ADD *.tar /
RUN ln -s /updatarium-cli*/ /updatarium-cli

WORKDIR /updatarium-cli

ENTRYPOINT ["/updatarium-cli/bin/updatarium-cli"]
CMD ["--help"]