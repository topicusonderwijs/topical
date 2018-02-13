FROM alpine:3.5

EXPOSE 80

WORKDIR /tmp

#CMD ["httpd", "-f", "-c", "/etc/httpd.conf", "-h", "/var/www/html/"]
CMD ["httpd", "-f", "-h", "/var/www/html/"]

#COPY httpd.conf /etc/httpd.conf

COPY index.html /var/www/html/
COPY overview.html /var/www/html/
