# Flutter FileReader
[![pub package](https://img.shields.io/pub/v/flutter_filereader.svg)](https://pub.dartlang.org/packages/flutter_filereader)

##### A local file view widget,Support a variety of file types, such as Doc Eexcl PPT TXT and so on,Android is implemented by Tencent X5（Not Support GooglePlay）,iOS is implemented by WKWebView


## Depend on it
Add this to your package's pubspec.yaml file:

1.9.1
```
dependencies:
  flutter_filereader: ^1.0.0
```
1.12.x
```
dependencies:
  flutter_filereader: ^2.2.0
```

2.0.0
```
dependencies:
  flutter_filereader: 3.0.0
```


## Support File Type
* IOS `docx,doc,xlsx,xls,pptx,ppt,pdf,txt,jpg,jpeg,png`
* Android `docx,doc,xlsx,xls,pptx,ppt,pdf,txt`

## Usage

### iOS
Make sure you add the following key to Info.plist for iOS
```
<key>io.flutter.embedded_views_preview</key><true/>
```

### Example

1. Initialize SDK

   ```dart
   void main() async {
     WidgetsFlutterBinding.ensureInitialized();
     await FileReader().initSdk('licenseKey');
     runApp(MyApp());
   }
   ```

2. FileReaderView

```
import 'package:flutter/material.dart';
import 'package:flutter_filereader/flutter_filereader.dart';

class FileReaderPage extends StatefulWidget {
  final String filePath;

  FileReaderPage({Key: Key, this.filePath});

  @override
  _FileReaderPageState createState() => _FileReaderPageState();
}

class _FileReaderPageState extends State<FileReaderPage> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("doc"),
      ),
      body: FileReaderView(
        filePath: widget.filePath,
      ),
    );
  }
}
```


## 注意事项
1. Not Support GooglePlay
2. 不支持在Android模拟器上运行
3. txt文档如果显示乱码,请将txt文档编码改成gbk
4. 由于TBS SDK即将下线，新版插件将引用腾讯浏览服务SDK，具体接入参考[ 腾讯浏览服务SDK 接入指南](https://cloud.tencent.com/document/product/1645/83900)

## 参考

- [腾讯浏览服务](https://cloud.tencent.com/document/product/1645)
- [关于腾讯浏览服务内核SDK-内核文档能力调整公告](https://mp.weixin.qq.com/s/rmSa4Fs77MDdjFioRKwXPA)