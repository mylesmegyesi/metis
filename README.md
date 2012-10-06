metis [mee'-tis]
=============

Metis is a library for data validation in [Clojure](http://clojure.org/) inspired by [Active Record Validations](http://guides.rubyonrails.org/active_record_validations_callbacks.html). Validations are used to ensure that the data coming from user input is valid. For example, when a user inputs their email address, it is important to ensure that the email looks like an email (test@test.com).

## Installation

``` clojure
:dependencies [[metis "0.2.1"]]
```

## Usage

### Defining a Validator using defvalidator

defvalidator is a macro that allows you to quickly define a validator function. There are many ways to use the defvalidator dsl to define your validation rules. Let's look at all the possible ways.

#### Single Attribute and Single Validator

##### no options

```clojure
(use 'metis.core)

(defvalidator user-validator
  (:first-name :presence))
```

##### with options

```clojure
(defvalidator user-validator
  (:first-name :presence {:message "Please input your first name."}))
```

#### Multiple Attributes and Single Validator

##### no options

```clojure
(defvalidator user-validator
  ([:first-name :last-name] :presence))
```

##### with options

```clojure
(defvalidator user-validator
  ([:first-name :last-name] :presence {:message "must have a first and last name silly!"}))
```

#### Single Attribute and Multiple Validators

##### no options

```clojure
(defvalidator user-validator
  (:first-name [:presence :length]))
```

##### with options

```clojure

(defvalidator user-validator
  (:first-name [:presence :length {:equal-to 5}]))

(defvalidator user-validator
  (:first-name [:presence {:message "gotta have it!"} :length]))

(defvalidator user-validator
  (:first-name [:presence {:message "gotta have it!"} :length {:equal-to 5}]))
```

#### Multiple Attributes and Multiple Validators

##### no options

```clojure
(defvalidator user-validator
  ([:first-name :last-name] [:presence :length]))
```

##### with options

```clojure

(defvalidator user-validator
  ([:first-name :last-name] [:presence :length {:equal-to 5}]))

(defvalidator user-validator
  ([:first-name :last-name] [:presence {:message "gotta have it!"} :length]))

(defvalidator user-validator
  ([:first-name :last-name] [:presence {:message "gotta have it!"} :length {:equal-to 5}]))
```

#### All together now

```clojure
(defvalidator user-validator
  ([:address :first-name :last-name :phone-number :email] :presence)
  (:first-name :with {:validator (fn [attr] false) :message "error!"})
  (:last-name :formatted {:pattern #"some pattern" :message "wrong formatting!"}))

(user-validator {:first-name nil :last-name "Smith" :phone-number "123456789" :email "snap.into@slim.jim"}
; {:address ("is not present"), :first-name ("error!" "is not present"), :last-name ("wrong formatting!")}
```

#### Shared Options:

These options are shared by all validators, custom or built-in.

* `:message` Provide a custom message upon failure.
* `:allow-nil`  Allow the value to be nil. Default `false`.
* `:allow-blank`  Allow the value to be blank (i.e. empty string or empty collection). Default `false`.
* `:allow-absence`  Allow the value to be blank or nil. Same as `:allow-blank true :allow-nil true`. Default `false`.
* `:only` Specifiy the contexts in which the validation should be run. Default `[]` (all contexts).
* `:except` Specifiy the contexts in which the validation should not be run. Default `[]` (no contexts).
* `:if` A function that takes a map and returns true if the validation should be run. Default `(fn [attrs] true)`.
* `:if-not` A function that takes map and returns true if the validation should not be run. Default `(fn [attrs] false)`false

### Defining custom Validators

Even though Metis has many [built-in validators](https://github.com/mylesmegyesi/metis/wiki/Built-in-validators), you will probably need to define your own at some point. Custom validators are defined in the same way that the built-in validators are defined, as functions.

A validator is simply a function that takes in a map and returns an error or nil. As an example, let's look at the built-in presence validator.

```clojure
(defn presence [map key _]
  (when-not (present? (get map key))
    "must be present")))
```

As you can see, this a very simple validator. It checks if the value is present and returns an error if it is not. This is structure of all the validators in Metis. Every validator takes in the map, the key to be validated, and a map of options. The presence validator, however, does not take in any options, so the third option is ignored.

Lets define a custom validator that checks if every charater is an 'a'.

```clojure
(defn all-a [map key options]
  (when-not (every? #(= "a" (str %)) (get map key))
    "not all a's"))

(all-a {:thing "aaa"} :thing {})
; nil

(all-a {:thing "abc"} :thing {})
; "not all a's"

(defvalidator first-name-with-only-a
  (:first-name :all-a))

(first-name-with-only-a {:first-name "aaa"})
;{}

(first-name-with-only-a {:first-name "abc"})
;{:first-name ("not all a's")}
```

### Composing Validators

In the same way that we can use custom validators within a defvalidator, we can also use previously defined validators.

```clojure
(defvalidator :country
  ([:code :name] :presence))

(defvalidator :address
  ([:line-1 :line-2 :zipcode] :presence)
  (:nation :country))

(defvalidator :person
  (:address :address)
  (:first-name :presence))

(person {})
; {:address {:zipcode ("must be present"), :line-2 ("must be present"), :line-1 ("must be present"), :nation {:name ("must be present"), :code ("must be present")}}, :first-name ("must be present")}

(person {:first-name "Myles" :address {:zipcode "60618" :line-1 "515 W Jackson Blvd." :line-2 "Floor 5" :nation {:code 1 :name "United States"}}})
; {}
```

### Contextual Validation

```clojure
(defvalidator user-validator
  (:first-name :presence {:only :creation :message "error!"})
  (:last-name :formatted {:pattern #"some pattern" :only [:updating :saving] :message "wrong formatting!"})
  (:address :presence {:message "You must have an address." :except [:updating]}))


(user-validator {}) ; when no context is specified, all validations are run
; {:first-name ("error!"), :last-name ("wrong formatting!"), :address ("You must have an address.")}

(user-validator {} :creation)
; {:first-name ("error!"), :address ("You must have an address.")}

(user-validator {} :updating)
; {:last-name ("wrong formatting!")}

(user-validator {} :saving)
; {:last-name ("wrong formatting!"), :address ("You must have an address.")}

(user-validator {} :somewhere-else)
; {:address ("You must have an address.")}
```

Note: the context names here are arbitrary; they can be anything.

### Conditional Validation

```clojure
(defn payment-type [attrs]
  (= (:payment-type attrs) "card"))

(defvalidator :if-conditional
  (:card-number :presence {:if payment-type}))

(defvalidator :if-not-conditional
  (:card-number :presence {:if-not payment-type}))

(if-conditional {})
; {}

(if-conditional {:payment-type "card"})
; {:card-number ("must be present")}

(if-not-conditional {})
; {:card-number ("must be present")}

(if-not-conditional {:payment-type "card"})
; {}
```

# Contributing

Clone the master branch, build, and run all the tests:

``` bash
git clone git@github.com:mylesmegyesi/metis.git
cd metis
lein deps
lein spec
```

Make patches and submit them along with an issue (see below).

# Issues

Post issues on the metis github project:

* [https://github.com/mylesmegyesi/metis/issues](https://github.com/mylesmegyesi/metis/issues)

# License

Copyright (C) 2012 Myles Megyesi All Rights Reserved.

Distributed under the Eclipse Public License, the same as Clojure.
