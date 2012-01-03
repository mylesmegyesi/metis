# validator

## Installation:

Leiningen:

```clojure
[metis/validator "0.1.1"]
```

Maven:

    <dependency>
      <groupId>metis</groupId>
      <artifactId>validator</artifactId>
      <version>0.1.1</version>
    </dependency>

## Usage

```clojure
(use '[metis.validator.core])

(defvalidator user-validator
    (validate [:address :first-name :last-name :phone-number :email] :presence)
    (validate :first-name :with {:validator (fn [attr] false) :message "error!"})
    (validate :last-name :formatted {:pattern #"some pattern" :message "wrong formatting!"})
    (validate :phone-number :phone-number)
    (validate :email :email))

(user-validator {:first-name nil :last-name "Smith" :phone-number "123456789" :email "snap.into@slim.jim"})
; {:last-name ["wrong formatting!"], :first-name ["is not present" "error!"], :address ["is not present"]}
```

## Built-in Validators

### acceptance

#### example

```clojure
(defvalidator usage-validator
    (validate :terms-of-service :acceptance))
	
(usage-validator {::terms-of-service "1"})
; {}

(defvalidator user-validator
    (validate :terms-of-service :acceptance {:accept "yes"}))

(user-validator {::terms-of-service "1"})	
; {:terms-of-service ["must be accepted"]}

```

#### options

##### :accept



## License

Copyright (C) 2011 Myles Megyesi

Distributed under the Eclipse Public License, the same as Clojure.
