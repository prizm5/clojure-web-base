#!/bin/sh 

gulp deleteDb --db $1
gulp createDb --db $1
gulp createViews --db $1
gulp publishAdmins --db $1
gulp publishRegistered --db $1
gulp publishCountries --db $1

gulp deleteDb --db $2
gulp createDb --db $2
gulp createViews --db $2
