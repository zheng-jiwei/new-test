Feature: Process
  運賃を検索する機能
  各種状況の運賃の正しさを確認する.

  Scenario: "北海道"までの通常運賃
    Given "非会員"の場合、購入商品の総額10000円の荷物を"北海道"まで運送する
    When 重さ<1kg
    Then 運賃は500円
    When 重さ>=1kg且つ重さ<2kg
    Then 運賃は600円
    When 重さ>=2kg且つ重さ<5kg
    Then 運賃は800円
    When 重さ>=5kg且つ重さ<10kg
    Then 運賃は1000円
    When 重さ>=10kg
    Then 運賃は1500円
    When 重さ=1.5kg
    Then 運賃は600円
    When 重さ=1kg
    Then 運賃は600円
    When 重さ=2kg
    Then 運賃は800円
    When 重さ=5kg
    Then 運賃は1000円
    When 重さ=10kg
    Then 運賃は1500円
    When 重さ=10.1kg
    Then 運賃は1500円

  Scenario: 購入金額<20000円の場合、"会員"なら運賃は20%OFF
    Given 購入商品の総額10000円、重さ10kgの荷物を"北海道"まで運送する
    When "会員"ユーザーと"非会員"ユーザーの運賃を比べる
    Then 比率は0.8

  Scenario: 20000円以上の商品を購入するなら運賃は無料
    Given "非会員"は重さ10kgの商品を"北海道"まで運送する
    When 購入商品の総額は20000円
    Then 送料は0円
    When 購入商品の総額は19999円
    Then 送料は1500円
    Given "会員"は重さ10kgの商品を"北海道"まで運送する
    When 購入商品の総額は20000円
    Then 送料は0円
