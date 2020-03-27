### テスト方法

最新のcommitは travis で lein test で通りました。  
テストコードは test/clojure/process_test.clj だけです。

lein cumberを試してみたい場合、
```
get reset --hard 9cf2c633408ed4347dcd07a40f3cbc06a2e3d597
```
でローカルのソースコードをロールバックしたら下記のコマンドで試してください。

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
