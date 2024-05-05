
import 'package:get_it/get_it.dart';
import 'package:olpaka/home/view_model.dart';

registerHome(){
  final l = GetIt.instance;
  l.registerFactory(() => HomeViewModel());
}