#!/usr/bin/env bash

##############################################################################
##
##  Script switch YH-Android apps for UN*X
##
##############################################################################

case "$1" in
<<<<<<< HEAD
  yonghui|shengyiplus|qiyoutong|yonghuitest|test)
=======
  yonghui|shengyiplus|qiyoutong|test)
>>>>>>> fa4ffd90cd3f6a0db797ede37e42becda41540e9
    # bundle exec ruby config/app_keeper.rb --app=shengyiplus --gradle --mipmap --manifest --res --java --apk --pgyer
    bundle exec ruby config/app_keeper.rb --app="$1" --gradle --mipmap --manifest --res --java
  ;;
  pgyer)
    bundle exec ruby config/app_keeper.rb --app="$(cat .current-app)" --apk --pgyer
  ;;
  github)
    bundle exec ruby config/app_keeper.rb --github
  ;;
  deploy)
    bash "$0" shengyiplus
    bash "$0" pgyer
    bash "$0" qiyoutong
    bash "$0" pgyer
    bash "$0" yonghui
    bash "$0" pgyer
  ;;
  all)
    echo 'TODO'
  ;;
  *)
    if [[ -z "$1" ]]; then
      bundle exec ruby config/app_keeper.rb --check
    else
      echo "unknown argument - $1"
    fi
  ;;
esac