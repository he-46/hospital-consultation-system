import request from './index'

export function alipayPay(data) {
  return request.post('/alipay/pay', data)
}
