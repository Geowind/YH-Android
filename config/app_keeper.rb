#!/usr/bin/env ruby
# encoding: utf-8
#
# ## 调整事项:
# 1. 应用图标
# 2. 应用名称XML档
# 3. Gradle应用ID
# 4. AndroidManifest 友盟、蒲公英配置
# 5. PrivateURLs 服务器域名
# 
# $ bundle exec ruby config/app_keeper.rb -h
# usage: config/app_keeper.rb [options]
#     -h, --help      print help info
#     -g, --gradle    bundle.gradle
#     -m, --mipmap    update mipmap
#     -x, --manifest  AndroidManifest.xml
#     -r, --res       res/strings.xml
#     -j, --java      PrivateURLs.java
#     -f, --apk       whether generate apk
#     -p, --pgyer     whether upload to pgyer
#     -v, --version   print the version
#     -a, --app       current app
#
require 'json'
require 'slop'
require 'nokogiri'
require 'settingslogic'
require 'active_support'
require 'active_support/core_ext/hash'
require 'active_support/core_ext/string'
require 'active_support/core_ext/numeric'

def exit_when condition, &block
  return unless condition
  yield
  exit
end

def xml_meta_data_sub(content, doc, key, value)
  meta_data = doc.xpath(%(//meta-data[@android:name='#{key}'])).first
  meta_data_value = meta_data.attributes['value'].value
  content.sub(meta_data_value, value)
end

def xml_string_sub(content, doc, key, value)
<<<<<<< HEAD
  meta_datas = doc.xpath(%(//string[@name='#{key}']))
  if meta_datas && meta_datas.first
    meta_data_value = meta_datas.first.text
    content = content.sub(meta_data_value, value)
  else
    puts %(#{key} not found; #{value})
  end

  content
end

slop_opts = Slop.parse do |o|
  o.string '-a', '--app', 'current app'
=======
  meta_data_value = doc.xpath(%(//string[@name='#{key}'])).first.text
  content.sub(meta_data_value, value)
end

slop_opts = Slop.parse do |o|
  o.string '-a', '--app', 'current app', default: 'yonghui'
>>>>>>> fa4ffd90cd3f6a0db797ede37e42becda41540e9
  o.bool '-g', '--gradle', 'bundle.gradle', default: false
  o.bool '-m', '--mipmap', 'update mipmap', default: false
  o.bool '-x', '--manifest', 'AndroidManifest.xml', default: false
  o.bool '-r', '--res', 'res/strings.xml', default: false
  o.bool '-j', '--java', 'PrivateURLs.java', default: false
  o.bool '-f', '--apk', 'whether generate apk', default: false
  o.bool '-p', '--pgyer', 'whether upload to pgyer', default: false
  o.bool '-b', '--github', 'black private info when commit', default: false
  o.bool '-c', '--check', 'info mirror', default: false
  o.on '-v', '--version', 'print the version' do
    puts Slop::VERSION
    exit
  end
  o.on '-h', '--help', 'print help info' do
    puts o
    exit
  end
end

<<<<<<< HEAD
current_app = slop_opts[:app] || `cat .current-app`.strip
bundle_display_hash = {
  yonghui: '永辉生意人',
  yonghuitest: '永辉应用(测试)',
=======
current_app = slop_opts[:app]
bundle_display_hash = {
  yonghui: '永辉生意人',
>>>>>>> fa4ffd90cd3f6a0db797ede37e42becda41540e9
  qiyoutong: '企邮通',
  shengyiplus: '生意+'
}
bundle_display_names = bundle_display_hash.keys.map(&:to_s)
exit_when !bundle_display_names.include?(current_app) do
  puts %(Abort: appname should in #{bundle_display_names}, but #{current_app})
end

current_app_name = bundle_display_hash.fetch(current_app.to_sym)

`echo '#{current_app}' > .current-app`
puts %(\n# current app: #{current_app}\n)

NAME_SPACE = current_app # TODO: namespace(variable_instance)
class Settings < Settingslogic
  source 'config/config.yaml'
  namespace NAME_SPACE
end

#
# reset app/build.gradle
#
if slop_opts[:gradle]
  gradle_path = 'app/build.gradle'
  gradle_text = IO.read(gradle_path)
  gradle_lines = gradle_text.split(/\n/)
  application_id_line = gradle_lines.find { |line| line.include?('applicationId') }
  application_id = application_id_line.strip.scan(/applicationId\s+'(com\.intfocus\..*?)'/).flatten[0]
  new_application_id_line = application_id_line.sub(application_id, Settings.application_id)

  puts %(- done: applicationId: #{application_id} => #{Settings.application_id})
  File.open(gradle_path, 'w:utf-8') do |file|
    file.puts gradle_text.sub(application_id_line, new_application_id_line)
  end
end

#
# reset mipmap and loading.zip
#
if slop_opts[:mipmap]
  puts %(- done: launcher@mipmap)
  `rm -fr app/src/main/res/mipmap-* && cp -fr config/Assets/mipmap-#{current_app}/mipmap-* app/src/main/res/`
  puts %(- done: loading zip)
  `cp -f config/Assets/loading-#{current_app}.zip app/src/main/assets/loading.zip`
  puts %(- done: banner_logo)
<<<<<<< HEAD
  `cp -f config/Assets/drawable-#{current_app}/*.png app/src/main/res/drawable/`
=======
  `cp -f config/Assets/banner-logo-#{current_app}.png app/src/main/res/drawable/banner_logo.png`
  puts %(- done: banner_setting)
  `cp -f config/Assets/banner-setting-#{current_app}.png app/src/main/res/drawable/banner_setting.png`
>>>>>>> fa4ffd90cd3f6a0db797ede37e42becda41540e9
end

#
# reset app/src/main/AndroidManifest.xml
#
manifest_xml_path = 'app/src/main/AndroidManifest.xml'
if slop_opts[:manifest]
  manifest_content = File.read(manifest_xml_path)
  manifest_nokogiri = Nokogiri.XML(manifest_content)
  manifest_content = xml_meta_data_sub(manifest_content, manifest_nokogiri, 'PGYER_APPID', Settings.pgyer.android)
  manifest_content = xml_meta_data_sub(manifest_content, manifest_nokogiri, 'UMENG_APPKEY', Settings.umeng.android.app_key)
  manifest_content = xml_meta_data_sub(manifest_content, manifest_nokogiri, 'UMENG_MESSAGE_SECRET', Settings.umeng.android.umeng_message_secret)

  puts %(- done: umeng/pgyer configuration)
  File.open(manifest_xml_path, 'w:utf-8') do |file|
    file.puts(manifest_content)
  end
end

#
# blurred app/src/main/AndroidManifest.xml 
#
if slop_opts[:github]
  manifest_content = File.read(manifest_xml_path)
  manifest_nokogiri = Nokogiri.XML(manifest_content)
  manifest_content = xml_meta_data_sub(manifest_content, manifest_nokogiri, 'PGYER_APPID', 'pgyer-app-id')
  manifest_content = xml_meta_data_sub(manifest_content, manifest_nokogiri, 'UMENG_APPKEY', 'umeng-app-key')
  manifest_content = xml_meta_data_sub(manifest_content, manifest_nokogiri, 'UMENG_MESSAGE_SECRET', 'umeng-message-secret')

  puts %(- done: umeng/pgyer blurred info)
  File.open(manifest_xml_path, 'w:utf-8') do |file|
    file.puts(manifest_content)
  end
end

#
# reset res/strings.xml
#
strings_xml_path = 'app/src/main/res/values/strings.xml'
if slop_opts[:res]
  strings_content = File.read(strings_xml_path)
  manifest_nokogiri = Nokogiri.XML(strings_content)
  strings_content = xml_string_sub(strings_content, manifest_nokogiri, 'app_name', current_app_name)
  strings_content = xml_string_sub(strings_content, manifest_nokogiri, 'title_activity_main', current_app_name)
<<<<<<< HEAD
  strings_content = xml_string_sub(strings_content, manifest_nokogiri, 'login_slogan_text', Settings.slogan_text)
=======
>>>>>>> fa4ffd90cd3f6a0db797ede37e42becda41540e9

  puts %(- done: res/strings.xml: #{current_app_name})
  File.open(strings_xml_path, 'w:utf-8') do |file|
    file.puts(strings_content)
  end
end

#
# check manifest.xml, res/strings.xml
#
if slop_opts[:check]
  def info_when_check(doc, key, expect_value, value_info)
    value = doc.xpath(%(//meta-data[@android:name='#{key}'])).first.attributes['value'].value
    puts %(- #{'**NOT**' if value != expect_value}match: #{value_info})
  end

  manifest_nokogiri = Nokogiri.XML(File.read(manifest_xml_path))
  info_when_check(manifest_nokogiri, 'PGYER_APPID', Settings.pgyer.android, 'pgyer app id')
  info_when_check(manifest_nokogiri, 'UMENG_APPKEY', Settings.umeng.android.app_key, 'umeng app id')
  info_when_check(manifest_nokogiri, 'UMENG_MESSAGE_SECRET', Settings.umeng.android.umeng_message_secret, 'umeng message secret')

  strings_nokogiri = Nokogiri.XML(File.read(strings_xml_path))
  app_name_value = strings_nokogiri.xpath(%(//string[@name='app_name'])).first.text
  puts %(- #{'**NOT**' if app_name_value != current_app_name}match: strings.xml app name)
end

if slop_opts[:java]
  puts %(- done: PrivateURLs java class)
  File.open('app/src/main/java/com/intfocus/yh_android/util/PrivateURLs.java', 'w:utf-8') do |file|
    file.puts <<-EOF.strip_heredoc
      //  PrivateURLs.java
      //
      //  `bundle install`
      //  `./appkeeper.sh #{current_app}`
      //
      //  Created by lijunjie on 16/06/02.
      //  Copyright © 2016年 com.intfocus. All rights reserved.
      //

      // current app: [#{current_app}]
      // automatic generated by app_keeper.rb at #{Time.now.strftime("%y/%m/%d %H:%M")}
      package com.intfocus.yh_android.util;

      public class PrivateURLs {
        public final static String kBaseUrl      = "#{Settings.server}";
        public final static String kBaseUrl1     = "http://10.0.3.2:4567";

        public final static String kPgyerAppId   = "#{Settings.pgyer.android}";
        public final static String kPgyerUrl     = "http://www.pgyer.com/#{Settings.key_store.alias}-a";
        
        public final static String kUMAppId      = "#{Settings.umeng.android.app_key}";
        public final static String kWXAppId      = "#{Settings.umeng_weixin.android.app_id}";
        public final static String kWXAppSecret  = "#{Settings.umeng_weixin.android.app_secret}";

<<<<<<< HEAD
        public final static boolean kDropMenuScan     = #{Settings.display_status.drop_menu_scan == 1 ? 'true' : 'false'};
        public final static boolean kDropMenuSearch   = #{Settings.display_status.drop_menu_search == 1 ? 'true' : 'false'};
        public final static boolean kDropMenuVoice    = #{Settings.display_status.drop_menu_voice == 1 ? 'true' : 'false'};
        public final static boolean kDropMenuUserInfo = #{Settings.display_status.drop_menu_user_info == 1 ? 'true' : 'false'};
        
        public final static boolean kTabBar        = #{Settings.display_status.tab_bar == 1 ? 'true' : 'false'};
        public final static boolean kTabBarKPI     = #{Settings.display_status.tab_bar_kpi == 1 ? 'true' : 'false'};
        public final static boolean kTabBarAnalyse = #{Settings.display_status.tab_bar_analyse == 1 ? 'true' : 'false'};
        public final static boolean kTabBarApp     = #{Settings.display_status.tab_bar_app == 1 ? 'true' : 'false'};
        public final static boolean kTabBarMessage = #{Settings.display_status.tab_bar_message == 1 ? 'true' : 'false'};
        
        public final static boolean kSubjectComment = #{Settings.display_status.subject_comment == 1 ? 'true' : 'false'};
        public final static boolean kSubjectShare   = #{Settings.display_status.subject_share == 1 ? 'true' : 'false'};
=======
        public final static boolean kDashboardTabBarDisplay        = #{Settings.display_status.tab_bar == 1 ? 'true' : 'false'};
        public final static boolean kDashboardTabBarDisplayKPI     = #{Settings.display_status.kpi == 1 ? 'true' : 'false'};
        public final static boolean kDashboardTabBarDisplayAnalyse = #{Settings.display_status.analyse == 1 ? 'true' : 'false'};
        public final static boolean kDashboardTabBarDisplayApp     = #{Settings.display_status.app == 1 ? 'true' : 'false'};
        public final static boolean kDashboardTabBarDisplayMessage = #{Settings.display_status.message == 1 ? 'true' : 'false'};
        public final static boolean kDashboardDisplayScanCode      = #{Settings.display_status.scan_code == 1 ? 'true' : 'false'};
        public final static boolean kSubjectDisplayComment         = #{Settings.display_status.comment == 1 ? 'true' : 'false'};
        public final static boolean kSubjectDisplayShare           = #{Settings.display_status.share == 1 ? 'true' : 'false'};
>>>>>>> fa4ffd90cd3f6a0db797ede37e42becda41540e9
      }
      EOF
  end
end

#
# gradlew generate apk
# 
if slop_opts[:apk]
  apk_path = 'app/build/outputs/apk/app-release.apk'
  key_store_path = File.join(Dir.pwd, Settings.key_store.path)
  exit_when !File.exist?(key_store_path) do
    puts %(Abort: key store file not exist - #{apk_path})
  end

  `test -f #{apk_path} && rm -f #{apk_path}`
  `export KEYSTORE=#{key_store_path} KEYSTORE_PASSWORD=#{Settings.key_store.password} KEY_ALIAS=#{Settings.key_store.alias} KEY_PASSWORD=#{Settings.key_store.alias_password} && /bin/bash ./gradlew assembleRelease`

  exit_when !File.exist?(apk_path) do
    puts %(Abort: failed generate apk - #{apk_path})
  end

  puts %(- done: generate apk(#{File.size(apk_path).to_s(:human_size)}) - #{apk_path})
end

if slop_opts[:pgyer]
  response = `curl --silent -F "file=@#{apk_path}" -F "uKey=#{Settings.pgyer.user_key}" -F "_api_key=#{Settings.pgyer.api_key}" http://www.pgyer.com/apiv1/app/upload`

<<<<<<< HEAD
  begin
    hash = JSON.parse(response).deep_symbolize_keys[:data]
    puts %(- done: upload apk(#{hash[:appFileSize].to_i.to_s(:human_size)}) to #pgyer#\n\t#{hash[:appName]}\n\t#{hash[:appIdentifier]}\n\t#{hash[:appVersion]}(#{hash[:appVersionNo]})\n\t#{hash[:appQRCodeURL]})
  rescue => e
    puts response.inspect
    puts e.message
  end
=======
  hash = JSON.parse(response).deep_symbolize_keys[:data]
  puts %(- done: upload apk(#{hash[:appFileSize].to_i.to_s(:human_size)}) to #pgyer#\n\t#{hash[:appName]}\n\t#{hash[:appIdentifier]}\n\t#{hash[:appVersion]}(#{hash[:appVersionNo]})\n\t#{hash[:appQRCodeURL]})
>>>>>>> fa4ffd90cd3f6a0db797ede37e42becda41540e9
end
