### テスト方法
- jdk1.8の場合
  - BDDでのテスト
    ```
    lein cucumber "test/features"
    ```

  または
  - TDDでのテスト
    ```
    lein test
    ```
- openjdk9以後の場合
    ```
    lein test
    ```
