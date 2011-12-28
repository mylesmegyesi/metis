# validator

## Installation:

Leiningen:

```clojure
[org.clojars.mylesmegyesi/validator "0.0.1-SNAPSHOT"]
```

Maven:

    <dependency>
      <groupId>org.clojars.mylesmegyesi</groupId>
      <artifactId>validator</artifactId>
      <version>0.0.1-SNAPSHOT</version>
    </dependency>

## Usage

```clojure
(use '[validator.core :only [validate]])

(validate {:first-name nil :last-name "Smith" :phone-number "123456789" :email "snap.into@slim.jim"}
    [:address :is-present]
    [:first-name :is-present {:message "not here!"}]
    [:first-name :with {:validator (fn [attr] false) :message "error!"}]
    [:last-name :is-present]
    [:last-name :is-formatted {:pattern #"some pattern" :message "wrong formatting!"}]
    [:phone-number :is-present]
    [:phone-number :is-phone-number]
    [:email :is-present]
    [:email :is-email])
    
; {:last-name "wrong formatting!", :first-name "not here!", :address "is not present"}
```

## Built-in Validators

* is-email - validates that the attribute is a valid email.
* is-phone-number - validates that the attribute is a valid phone number.
* is-formatted - validates the formatting of the attribute. The :pattern argument is required.
* with - validates the attributes with a given function. The function should take one argument and return a boolean.

All validators accept custom error message with the :message argument.

## License

Copyright (C) 2011 Myles Megyesi

Distributed under the Eclipse Public License, the same as Clojure.
