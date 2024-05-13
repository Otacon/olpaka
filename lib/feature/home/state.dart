class HomeState {
  final HomeTabChat chat;
  final HomeTabDownloads downloads;
  final HomeTabSettings settings;

  HomeState(this.chat, this.downloads, this.settings);
}

sealed class HomeTab {
  final bool isSelected;

  HomeTab(this.isSelected);
}

class HomeTabChat extends HomeTab {
  HomeTabChat(super.isSelected);
}

class HomeTabDownloads extends HomeTab {
  final DownloadsState state;

  HomeTabDownloads(super.isSelected, this.state);
}

class HomeTabSettings extends HomeTab {
  HomeTabSettings(super.isSelected);
}

enum DownloadsState { none, downloading, completed }
