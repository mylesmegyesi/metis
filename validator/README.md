# validator

## Usage

```clojure
(use '[metis.validator.core])

(defvalidator user-validator
    ([:address :first-name :last-name :phone-number :email] :presence)
    (:first-name :with {:validator (fn [attr] false) :message "error!"})
    (:last-name :formatted {:pattern #"some pattern" :message "wrong formatting!"}))

(user-validator {:first-name nil :last-name "Smith" :phone-number "123456789" :email "snap.into@slim.jim"}
; {:address ("is not present"), :first-name ("error!" "is not present"), :last-name ("wrong formatting!")}
```

## Built-in Validators

## with

```clojure
(defvalidator location-validator
    (:gps :with {:message "bad gps!" :validator (fn [attrs] (not (or (nil? (:latitude attrs)) (nil? (:longitude attrs)))))}))

(location-validator {:latitude "" :longitude nil})
; {:gps ("bad gps!")}

(location-validator {:latitude "90" :longitude "20"})
; {}
```

### Options:

* `:validator`  The validator to run against the record. This function should accept a map and return a boolean. Default `nil`.

### Default error message:

"is invalid"

## presence

```clojure
(defvalidator user-validator
    (:name :presence))

(user-validator {:name nil})
; {:name ("is not present")}

(user-validator {:name ""})
; {:name ("is not present")}

(user-validator {:name "Jimmy John's"})
; {}
```

### Options:

None.

### Default error message:

"is invalid"

## acceptance

```clojure
(defvalidator usage-validator
    (:terms-of-service :acceptance))
	
(usage-validator {:terms-of-service "1"})
; {}

(defvalidator usage-validator
    (:terms-of-service :acceptance {:accept "yes"}))

(usage-validator {:terms-of-service "1"})
; {:terms-of-service ("must be accepted")}

(usage-validator {:terms-of-service "yes"})
; {}
```

### Options:

* `:accept`  The value to compare against. Default `"1"`.

### Default error message:

"must be accepted"

## confirmation

```clojure
(defvalidator user-validator
    (:email :confirmation))
	
(user-validator {:email "snap.into@slim.jim"})
; {:email ("doesn't match confirmation")}

(user-validator {:email "snap.into@slim.jim" :email-confirmation "snap.into@slim.jim"})
; {}

(defvalidator user-validator
    (:email :confirmation {:confirm :some-other}))

(user-validator {:email "snap.into@slim.jim" :email-confirmation "snap.into@slim.jim"})
; {:email ("doesn't match confirmation")}

(user-validator {:email "snap.into@slim.jim" :some-other "snap.into@slim.jim"})
; {}
```

### Options:

* `:confirm`  The key to compare against. Default `(attr)-conifrmation`.

### Default error message:

"doesn't match confirmation"

## numericality

```clojure
(defvalidator user-validator
    (:age :numericality))
	
(user-validator {:age 10})
; {}

(user-validator {:age "asf"})
; {:age ("is not a number")}

(defvalidator user-validator
    (:age :numericality {:only-integer true}))

(user-validator {:age 10})
; {}

(user-validator {:age 10.0})
; {:age ("is not an integer")}

(defvalidator user-validator
    (:age :numericality {:greater-than 18}))

(user-validator {:age 10})
; {:age ("is not greater than 18")}

(user-validator {:age 19})
; {}
```

### Options:

* `:only-integer`  Accept only Integers. Default `(attr)-conifrmation`.
* `not-an-int`  The error message to supply if the `:only-integer` validation fails. Default `"is not an integer"`.
* `:greater-than` Value must be greater than this value. Default `nil`.
* `:not-greater-than`  The error message to supply if the `:greater-than` validation fails. Default `"is not greater than (value)"`.
* `:greater-than-or-equal-to`  Value must be greater than or equal to this value. Default `nil`.
* `:not-greater-than-or-equal-to`  The error message to supply if the `:greater-than-or-equal-to` validation fails. Default `"is not greater than or equal to (value)"`.
* `:equal-to`  Value must be equal to this value. Default `nil`.
* `:not-equal-to`  The error message to supply if the `:equal-to` validation fails. Default `"is equal to (value)"`.
* `:less-than` Value must be less than this value. Default `nil`.
* `:not-less-than`  The error message to supply if the `:less-than` validation fails. Default `"is not less than (value)"`.
* `:less-than-or-equal-to`  Value must be less than or equal to this value. Default `nil`.
* `:not-less-than-or-equal-to`  The error message to supply if the `:less-than-or-equal-to` validation fails. Default `"is not less than or equal to (value)"`.
* `:odd` Value must be odd. Default `false`.
* `:not-odd` The error message to supply if the `:odd` validation fails. Default `"is not odd"`.
* `:even` Value must be even. Default `false`.
* `:not-even` The error message to supply if the `:even` validation fails. Default `"is not even"`.
* `:in` Value must be in the given collection. Default `nil`.
* `:not-in` The error message to supply if the `:in` validation fails. Default `"is not included in the list"`.

### Default error message:

"is not a number"

## length

Takes the `count` of the attribute and applies the numericallity validator to it.

```clojure
(defvalidator user-validator
    (:zipcode :length {:equal-to 5}))

(user-validator {:zipcode "1234"})
; {:zipcode ("is not equal to 5")}

	
(user-validator {:zipcode "12345"})
; {}
```

### Options:

Same as numericality.  

### Default error message:

Same as numericality.

## inclusion

```clojure
(defvalidator user-validator
    (:color :inclusion {:in [:blue :orange]}))

(user-validator {:color :pink})
; {:color ("is not included in the list")}
	
(user-validator {:color :blue})
; {}
```

### Options:

* `:in` Value must be in the given collection. Default `[]`.

### Default error message:

"is not included in the list"

## exclusion

```clojure
(defvalidator user-validator
    (:color :exclusion {:from [:blue :orange]}))

(user-validator {:color :pink})
; {}
	
(user-validator {:color :blue})
; {:color ("is reserved")}
```

### Options:

* `:from` Value must be not in the given collection. Default `[]`.

### Default error message:

"is reserved"

## formatted

```clojure
(defvalidator user-validator
    (:name :formatted {:pattern #"[A-Z]+" :message "is not capitalized"}))

(user-validator {:name "Dave"})
; {:name ("is not capitalized")}
	
(user-validator {:name "DAVE"})
; {}
```  
  
### Options:  

* `:pattern` A Regex Pattern to match against. Default `#""`.

### Default error message:

"has the incorrect format"

## Common Options:

* `:message` Provide a custom messag upon failure.
* `:allow-nil`  Allow the value to be nil. Default `false`.
* `:allow-blank`  Allow the value to be blank (i.e. empty string or empty collection). Default `false`.
* `:on` Specifiy the context in which the validation should be run. Default `[:create :update]`.

## License

Copyright (C) 2012 Myles Megyesi

Distributed under the Eclipse Public License, the same as Clojure.
