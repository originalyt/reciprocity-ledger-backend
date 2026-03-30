import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../shared/models/ledger_models.dart';
import '../../../../shared/providers/mock_providers.dart';

class QuickAddContactDialog extends ConsumerStatefulWidget {
  const QuickAddContactDialog({super.key});

  @override
  ConsumerState<QuickAddContactDialog> createState() => _QuickAddContactDialogState();
}

class _QuickAddContactDialogState extends ConsumerState<QuickAddContactDialog> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _aliasController = TextEditingController();
  final _salutationController = TextEditingController();
  final _mobileController = TextEditingController();
  final _remarkController = TextEditingController();

  String? _selectedRelationTypeCode;
  bool _submitting = false;

  @override
  void dispose() {
    _nameController.dispose();
    _aliasController.dispose();
    _salutationController.dispose();
    _mobileController.dispose();
    _remarkController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final relationTypesAsync = ref.watch(relationTypesProvider);

    return AlertDialog(
      title: const Text('新建联系人'),
      content: SizedBox(
        width: double.maxFinite,
        child: Form(
          key: _formKey,
          child: ListView(
            shrinkWrap: true,
            children: [
              TextFormField(
                controller: _nameController,
                decoration: const InputDecoration(labelText: '姓名 *'),
                validator: (value) {
                  if (value == null || value.trim().isEmpty) {
                    return '请输入姓名';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _aliasController,
                decoration: const InputDecoration(labelText: '别名'),
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _salutationController,
                decoration: const InputDecoration(labelText: '称呼'),
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _mobileController,
                keyboardType: TextInputType.phone,
                decoration: const InputDecoration(labelText: '手机号'),
              ),
              const SizedBox(height: 16),
              relationTypesAsync.when(
                loading: () => const Center(child: CircularProgressIndicator()),
                error: (error, stack) => const Text('加载关系类型失败'),
                data: (relationTypes) {
                  if (_selectedRelationTypeCode == null && relationTypes.isNotEmpty) {
                    _selectedRelationTypeCode = relationTypes.first.code;
                  }
                  return Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text('关系类型 *', style: TextStyle(fontSize: 12)),
                      const SizedBox(height: 8),
                      Wrap(
                        spacing: 8,
                        runSpacing: 8,
                        children: relationTypes.map((item) {
                          return ChoiceChip(
                            label: Text(item.name),
                            selected: _selectedRelationTypeCode == item.code,
                            onSelected: (_) {
                              setState(() {
                                _selectedRelationTypeCode = item.code;
                              });
                            },
                          );
                        }).toList(),
                      ),
                    ],
                  );
                },
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _remarkController,
                maxLines: 2,
                decoration: const InputDecoration(labelText: '备注'),
              ),
            ],
          ),
        ),
      ),
      actions: [
        TextButton(
          onPressed: _submitting ? null : () => Navigator.of(context).pop(),
          child: const Text('取消'),
        ),
        FilledButton(
          onPressed: _submitting ? null : _submit,
          child: _submitting
              ? const SizedBox(
                  width: 16,
                  height: 16,
                  child: CircularProgressIndicator(strokeWidth: 2),
                )
              : const Text('保存'),
        ),
      ],
    );
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) {
      return;
    }
    if (_selectedRelationTypeCode == null) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('请选择关系类型')));
      return;
    }

    setState(() {
      _submitting = true;
    });

    try {
      final repository = ref.read(ledgerRepositoryProvider);
      final result = await repository.quickSaveContact(
        ContactSaveDraft(
          name: _nameController.text.trim(),
          relationTypeCode: _selectedRelationTypeCode!,
          aliasName: _aliasController.text.trim().isEmpty ? null : _aliasController.text.trim(),
          salutation: _salutationController.text.trim().isEmpty ? null : _salutationController.text.trim(),
          mobile: _mobileController.text.trim().isEmpty ? null : _mobileController.text.trim(),
          remark: _remarkController.text.trim().isEmpty ? null : _remarkController.text.trim(),
        ),
      );

      if (!mounted) {
        return;
      }

      ref.invalidate(contactsProvider);

      Navigator.of(context).pop(result);
    } catch (error) {
      if (!mounted) {
        return;
      }
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(error.toString())));
    } finally {
      if (mounted) {
        setState(() {
          _submitting = false;
        });
      }
    }
  }
}
