// 信息类型
export const ITEM_TYPE = {
  LOST: 0,
  FOUND: 1
}

export const ITEM_TYPE_LABEL = {
  0: '失物',
  1: '拾物'
}

export const ITEM_TYPE_TAG = {
  0: 'warning',
  1: 'success'
}

// 状态文案（按类型区分 status=2）
export function getStatusLabel(status, itemType = 0) {
  const map = {
    0: '待审核',
    1: '已审核',
    2: itemType === 1 ? '已归还' : '已找回',
    3: '已过期'
  }
  return map[status] ?? '未知状态'
}

export function getStatusTagType(status) {
  const map = {
    0: 'warning',
    1: 'success',
    2: 'info',
    3: 'danger'
  }
  return map[status] ?? 'info'
}

// 地点/日期标签
export function getLocationLabel(itemType) {
  return itemType === 1 ? '拾获地点' : '丢失地点'
}

export function getDateLabel(itemType) {
  return itemType === 1 ? '拾获日期' : '丢失日期'
}

export function getContentPlaceholder(itemType) {
  return itemType === 1
    ? '请详细描述物品特征及拾获情况'
    : '请详细描述物品特征及丢失情况'
}

export function getMarkDoneLabel(itemType) {
  return itemType === 1 ? '设为已归还' : '设为已找回'
}
