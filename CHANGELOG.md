# Change Log

## [0.13.0](https://github.com/auth0/jwks-rsa-java/tree/0.13.0) (2020-08-26)
[Full Changelog](https://github.com/auth0/jwks-rsa-java/compare/0.12.0...0.13.0)

**Removed**
- remove commons-io dependency [\#95](https://github.com/auth0/jwks-rsa-java/pull/95) ([Jaxsun](https://github.com/Jaxsun))

## [0.12.0](https://github.com/auth0/jwks-rsa-java/tree/0.12.0) (2020-06-25)
[Full Changelog](https://github.com/auth0/jwks-rsa-java/compare/0.11.0...0.12.0)

**Added**
- Add NetworkException, docs and tests [\#89](https://github.com/auth0/jwks-rsa-java/pull/89) ([lbalmaceda](https://github.com/lbalmaceda))

## [0.11.0](https://github.com/auth0/jwks-rsa-java/tree/0.11.0) (2020-02-14)
[Full Changelog](https://github.com/auth0/jwks-rsa-java/compare/0.10.0...0.11.0)

**Changed**
- Update Gradle version to 6.1.1 [\#84](https://github.com/auth0/jwks-rsa-java/pull/84) ([PaulDaviesC](https://github.com/PaulDaviesC))

## [0.10.0](https://github.com/auth0/jwks-rsa-java/tree/0.10.0) (2020-02-11)
[Full Changelog](https://github.com/auth0/jwks-rsa-java/compare/0.9.0...0.10.0)

**Changed**
- Set default cache expiration time to 10 minutes [\#82](https://github.com/auth0/jwks-rsa-java/pull/82) ([jimmyjames](https://github.com/jimmyjames))

## [0.9.0](https://github.com/auth0/jwks-rsa-java/tree/0.9.0) (2019-09-26)
[Full Changelog](https://github.com/auth0/jwks-rsa-java/compare/0.8.3...0.9.0)

**Changed**
- Improve ObjectMapper use [\#68](https://github.com/auth0/jwks-rsa-java/pull/68) ([skjolber](https://github.com/skjolber))
- Concatenate JWKS path with existing path on domain if present [\#67](https://github.com/auth0/jwks-rsa-java/pull/67) ([ltj](https://github.com/ltj))

**Security**
- Update jackson-databind to address CVE [\#72](https://github.com/auth0/jwks-rsa-java/pull/72) ([jimmyjames](https://github.com/jimmyjames))

## [0.8.3](https://github.com/auth0/jwks-rsa-java/tree/0.8.3) (2019-08-15)
[Full Changelog](https://github.com/auth0/jwks-rsa-java/compare/0.8.2...0.8.3)

**Security**
- Update dependencies [\#65](https://github.com/auth0/jwks-rsa-java/pull/65) ([jimmyjames](https://github.com/jimmyjames))

## [0.8.2](https://github.com/auth0/jwks-rsa-java/tree/0.8.2) (2019-05-22)
[Full Changelog](https://github.com/auth0/jwks-rsa-java/compare/0.8.1...0.8.2)

**Security**
- Fix security issue with jackson-databind [\#63](https://github.com/auth0/jwks-rsa-java/pull/63) ([lbalmaceda](https://github.com/lbalmaceda))

## [0.8.1](https://github.com/auth0/jwks-rsa-java/tree/0.8.1) (2019-05-02)
[Full Changelog](https://github.com/auth0/jwks-rsa-java/compare/0.8.0...0.8.1)

**Fixed**
- Add content-type header to the jwks request [\#59](https://github.com/auth0/jwks-rsa-java/pull/59) ([lbalmaceda](https://github.com/lbalmaceda))

## [0.8.0](https://github.com/auth0/jwks-rsa-java/tree/0.8.0) (2019-03-28)
[Full Changelog](https://github.com/auth0/jwks-rsa-java/compare/0.7.0...0.8.0)

**Added**
- Expose getAll() publicly on UrlJwkProvider [\#38](https://github.com/auth0/jwks-rsa-java/pull/38) ([kampka](https://github.com/kampka))
- change visibility to public on fromValues method in Jwk [\#36](https://github.com/auth0/jwks-rsa-java/pull/36) ([underscorenico](https://github.com/underscorenico))

**Changed**
- Update guava to version 27.0.1-jre [\#54](https://github.com/auth0/jwks-rsa-java/pull/54) ([golszewski86](https://github.com/golszewski86))

## [0.7.0](https://github.com/auth0/jwks-rsa-java/tree/0.7.0) (2019-01-03)
[Full Changelog](https://github.com/auth0/jwks-rsa-java/compare/0.6.1...0.7.0)

**Changed**
- Throw correct exception when key is not of type RSA [\#48](https://github.com/auth0/jwks-rsa-java/pull/48) ([lbalmaceda](https://github.com/lbalmaceda))

**Security**
- Bump jackson-databind to patch security issues [\#49](https://github.com/auth0/jwks-rsa-java/pull/49) ([lbalmaceda](https://github.com/lbalmaceda))

## [0.6.1](https://github.com/auth0/jwks-rsa-java/tree/0.6.1) (2018-10-24)
[Full Changelog](https://github.com/auth0/jwks-rsa-java/compare/0.6.0...0.6.1)

**Security**
- Use latest jackson-databind dependency [\#43](https://github.com/auth0/jwks-rsa-java/pull/43) ([lbalmaceda](https://github.com/lbalmaceda))

## [0.6.0](https://github.com/auth0/jwks-rsa-java/tree/0.6.0) (2018-07-18)
[Full Changelog](https://github.com/auth0/jwks-rsa-java/compare/0.5.0...0.6.0)

**Changed**
- Optional kid on single item JWK sets [\#32](https://github.com/auth0/jwks-rsa-java/pull/32) ([lbalmaceda](https://github.com/lbalmaceda))

## [0.5.0](https://github.com/auth0/jwks-rsa-java/tree/0.5.0) (2018-06-13)
[Full Changelog](https://github.com/auth0/jwks-rsa-java/compare/0.4.0...0.5.0)
**Closed issues**
- Improve release procedure [\#29](https://github.com/auth0/jwks-rsa-java/issues/29)

**Added**
- Optional support for connection / read timeout [\#27](https://github.com/auth0/jwks-rsa-java/pull/27) ([skjolber](https://github.com/skjolber))

**Fixed**
- Improve release procedure [\#30](https://github.com/auth0/jwks-rsa-java/pull/30) ([lbalmaceda](https://github.com/lbalmaceda))

**Security**
- [Snyk] Fix for 6 vulnerable dependencies [\#28](https://github.com/auth0/jwks-rsa-java/pull/28) ([crew-security](https://github.com/crew-security))
- bump commons-io due to security vulnerabilities in that library [\#26](https://github.com/auth0/jwks-rsa-java/pull/26) ([ryber](https://github.com/ryber))

## [0.4.0](https://github.com/auth0/jwks-rsa-java/tree/jwks-rsa-0.4.0) (2018-04-27)
[Full Changelog](https://github.com/auth0/jwks-rsa-java/compare/jwks-rsa-0.3.0...jwks-rsa-0.4.0)

**Added**
- Added url constructor to JwkProviderBuilder [\#22](https://github.com/auth0/jwks-rsa-java/pull/22) ([darthvalinor](https://github.com/darthvalinor))

## [0.3.0](https://github.com/auth0/jwks-rsa-java/tree/jwks-rsa-0.3.0) (2017-11-10)
[Full Changelog](https://github.com/auth0/jwks-rsa-java/compare/jwks-rsa-0.2.0...jwks-rsa-0.3.0)

**Changed**
- Parse 'key_ops' as an Array rather than a String [\#13](https://github.com/auth0/jwks-rsa-java/pull/13) ([chadramsey](https://github.com/chadramsey))
- Remove algorithm (alg tag) from mandatory Jwk attributes [\#10](https://github.com/auth0/jwks-rsa-java/pull/10) ([Colin-b](https://github.com/Colin-b))

## [0.2.0](https://github.com/auth0/jwks-rsa-java/tree/jwks-rsa-0.2.0) (2016-12-05)
[Full Changelog](https://github.com/auth0/jwks-rsa-java/compare/jwks-rsa-0.1.0...jwks-rsa-0.2.0)

**Added**
- Add Rate Limit provider [\#1](https://github.com/auth0/jwks-rsa-java/pull/1) ([lbalmaceda](https://github.com/lbalmaceda))

**Changed**
- Refactor JwkProviderBuilder [\#2](https://github.com/auth0/jwks-rsa-java/pull/2) ([lbalmaceda](https://github.com/lbalmaceda))
- Replace ExecutorService with primitive counters. [\#3](https://github.com/auth0/jwks-rsa-java/pull/3) ([lbalmaceda](https://github.com/lbalmaceda))

## [0.1.0](https://github.com/auth0/jwks-rsa-java/tree/jwks-rsa-0.1.0) (2016-08-30)

JSON Web Token Set parser library for Java. Initial release.


