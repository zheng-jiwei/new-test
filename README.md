
### 構成説明
- テストシナリオ
  ```
  test/features/process.feature
  ```
- テストコードファイル
  ```
  test/features/step_definitions/process_steps.clj
  ```

- lein-cucumberを使ってるので、jdk1.8が必要です。jdk1.8とopenjdk11は同時インストール手順は下記のURLを参照してください
  ```
  https://www.linuxbabe.com/ubuntu/install-oracle-java-8-openjdk-11-ubuntu-18-04-18-10  
  ```

### テスト方法
  ```
　lein cucumber
  ```
