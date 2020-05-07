FROM alpine:3

RUN apk --no-cache add busybox-extras

EXPOSE 80

WORKDIR /tmp

CMD ["httpd", "-f", "-h", "/var/www/html/"]

#COPY httpd.conf /etc/httpd.conf

COPY index.html /var/www/html/
