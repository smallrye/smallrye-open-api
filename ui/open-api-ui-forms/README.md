A generic bundled spa in React, which introspects Swagger OpenAPI definitions file, to build automatically html forms (bootstrap4) for POST endpoints exchanging JSON

GOALs:
- build automatically an html form, by introspecting Swagger OpenAPI definitions file
- the introspection works at runtime, not at `npm` build time

non-GOALS:
- not a "low-code" app generator

# DEMO

Watch the video on YouTube:

[![Watch the video](https://img.youtube.com/vi/av_DoGNl2jI/hqdefault.jpg)](https://youtu.be/av_DoGNl2jI)

## Building and running on localhost

First install dependencies:

```sh
npm install
```

To create a production build:

```sh
npm run build-prod
```

To create a development build:

```sh
npm run build-dev
```

## Running

Open the file `dist/index.html` in your browser

## Deploying

To include it in your project (replace xyz with the latest version):

```
<dependency>
    <groupId>io.smallrye</groupId>
    <artifactId>smallrye-open-api-ui-forms</artifactId>
    <version>xyz</version>
    <scope>runtime</scope>
</dependency>
```

The UI will be available under `/openapi-ui-forms`.
