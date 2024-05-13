import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';
import 'package:flutter/services.dart';
import 'package:get_it/get_it.dart';
import 'package:olpaka/feature/chat/state.dart';
import 'package:olpaka/feature/chat/view_model.dart';
import 'package:olpaka/generated/l10n.dart';
import 'package:olpaka/ui/empty_screen.dart';
import 'package:olpaka/ui/loading.dart';
import 'package:olpaka/ui/markdown.dart';
import 'package:stacked/stacked.dart';

class ChatScreen extends StatelessWidget {
  ChatScreen({super.key});

  final ScrollController _scrollController = ScrollController();

  @override
  Widget build(BuildContext context) {
    return ViewModelBuilder<ChatViewModel>.reactive(
      viewModelBuilder: () => GetIt.I.get(),
      onViewModelReady: (viewModel) {
        viewModel.events.listen((event) {});
        viewModel.onCreate();
      },
      builder: (context, viewModel, child) {
        final state = viewModel.state;
        return Scaffold(
          appBar: AppBar(
            elevation: 4,
            shadowColor: Theme.of(context).shadowColor,
            centerTitle: true,
            title: Text(
              S.current.app_name,
              style: Theme.of(context).textTheme.headlineMedium,
            ),
          ),
          body: switch (state) {
            ChatStateLoading() => const Loading(),
            ChatStateContent() => _content(
                context,
                messages: state.messages,
                models: state.models,
                selectedModel: state.selectedModel,
                isEnabled: !state.isGeneratingMessage,
                onModelSelected: viewModel.onModelChanged,
                onSendMessage: viewModel.onSendMessage,
              ),
            ChatStateError() => EmptyScreen(
                header: state.title,
                body: state.message,
                ctaText: state.ctaText,
                onCtaClicked: viewModel.onRefresh,
              ),
          },
        );
      },
    );
  }

  Widget _content(
    BuildContext context, {
    required List<ChatMessage> messages,
    required List<ChatModel> models,
    required Function(String) onSendMessage,
    required Function(ChatModel?) onModelSelected,
    bool isEnabled = true,
    ChatModel? selectedModel,
  }) {
    SchedulerBinding.instance.addPostFrameCallback((_) {
      _scrollController.animateTo(
        _scrollController.position.maxScrollExtent,
        duration: const Duration(milliseconds: 3),
        curve: Curves.linear,
      );
    });

    final Widget content;
    if (messages.isEmpty) {
      content = EmptyScreen(
        header: S.current.chat_empty_screen_title,
        body: S.current.chat_empty_screen_message,
      );
    } else {
      content = ListView.builder(
        controller: _scrollController,
        itemCount: messages.length,
        itemBuilder: (context, index) {
          final message = messages[index];
          return switch (message) {
            ChatMessageUser() => _chatMessage(
                context,
                sender: S.current.chat_user_name,
                text: message.message,
              ),
            ChatMessageError() => Container(),
            ChatMessageAssistant() => _chatMessage(
                context,
                sender: S.current.chat_assistant_name,
                text: message.message,
                showLoading: message.isLoading,
              ),
          };
        },
      );
    }
    return Column(
      children: [
        Expanded(child: content),
        FractionallySizedBox(
          widthFactor: 0.9,
          child: _MessageInputBar(
            isEnabled: isEnabled,
            onSendMessage: onSendMessage,
            onModelSelected: onModelSelected,
            selectedModel: selectedModel,
            models: models,
          ),
        ),
      ],
    );
  }

  Widget _chatMessage(
    BuildContext context, {
    required String sender,
    required String text,
    bool showLoading = false,
  }) {
    return FractionallySizedBox(
      widthFactor: 0.75,
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 16.0),
        child: Card.outlined(
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisAlignment: MainAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Text(
                      sender,
                      style: Theme.of(context).textTheme.headlineSmall,
                    ),
                    const SizedBox(width: 16.0),
                    Visibility(
                      visible: showLoading,
                      child: const SizedBox(
                        width: 12,
                        height: 12,
                        child: CircularProgressIndicator(),
                      ),
                    )
                  ],
                ),
                Markdown(text, selectable: true)
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class _MessageInputBar extends StatefulWidget {
  final bool isEnabled;
  final ChatModel? selectedModel;
  final List<ChatModel> models;
  final Function(String) onSendMessage;
  final Function(ChatModel?) onModelSelected;

  const _MessageInputBar({
    required this.isEnabled,
    required this.onSendMessage,
    required this.models,
    required this.selectedModel,
    required this.onModelSelected,
  });

  @override
  State<StatefulWidget> createState() {
    return _MessageInputBarState();
  }
}

class _MessageInputBarState extends State<_MessageInputBar> {
  _MessageInputBarState();

  final TextEditingController _controller = TextEditingController();
  late FocusNode _focusNode;

  @override
  void initState() {
    _focusNode = FocusNode(
      onKeyEvent: (node, event) {
        final enterPressedWithoutShift = event is KeyDownEvent &&
            event.physicalKey == PhysicalKeyboardKey.enter &&
            !HardwareKeyboard.instance.physicalKeysPressed.any(
              (key) => <PhysicalKeyboardKey>{
                PhysicalKeyboardKey.shiftLeft,
                PhysicalKeyboardKey.shiftRight,
              }.contains(key),
            );

        if (enterPressedWithoutShift) {
          widget.onSendMessage(_controller.text);
          _controller.clear();
          return KeyEventResult.handled;
        } else if (event is KeyRepeatEvent) {
          return KeyEventResult.handled;
        } else {
          return KeyEventResult.ignored;
        }
      },
    );
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    final Function(ChatModel?)? dropdownCallback;
    if (widget.isEnabled) {
      _focusNode.requestFocus();
      dropdownCallback = widget.onModelSelected;
    } else {
      dropdownCallback = null;
    }
    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Expanded(
            child: TextField(
              maxLines: 3,
              minLines: 1,
              focusNode: _focusNode,
              controller: _controller,
              enabled: widget.isEnabled,
              onSubmitted: widget.onSendMessage,
              decoration: InputDecoration(
                suffixIcon: IconButton(
                  onPressed: () =>
                      {widget.onSendMessage(_controller.value.text)},
                  icon: const Icon(Icons.send),
                ),
                border: const OutlineInputBorder(),
                hintText: S.current.chat_text_input_hint,
              ),
            ),
          ),
          const SizedBox(width: 16.0),
          DropdownButton<ChatModel>(
            hint: Text(S.current.chat_model_dropdown_hint),
            onChanged: dropdownCallback,
            value: widget.selectedModel,
            items: widget.models
                .map((e) => DropdownMenuItem(value: e, child: Text(e.name)))
                .toList(),
          ),
        ],
      ),
    );
  }
}
