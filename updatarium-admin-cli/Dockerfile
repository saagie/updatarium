FROM openjdk:11

ADD *.tar /
RUN ln -s /updatarium-admin-cli*/ /updatarium-admin-cli

WORKDIR /updatarium-admin-cli

ENTRYPOINT ["/updatarium-admin-cli/bin/updatarium-admin-cli"]
CMD ["--help"]