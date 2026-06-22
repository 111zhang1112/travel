import { describe, it, expect } from 'vitest'

describe('HotelDetail - Price Calculation', () => {
  // 价格计算函数
  const calculateTotalPrice = (roomPrice, nights, quantity) => {
    return (roomPrice * nights * quantity).toFixed(2)
  }

  // 格式化价格显示
  const formatPrice = (price) => {
    return `¥${Number(price).toFixed(2)}`
  }

  it('should calculate price correctly for single room single night', () => {
    const price = calculateTotalPrice(688, 1, 1)
    expect(price).toBe('688.00')
  })

  it('should calculate price correctly for single room multiple nights', () => {
    const price = calculateTotalPrice(688, 3, 1)
    expect(price).toBe('2064.00')
  })

  it('should calculate price correctly for multiple rooms single night', () => {
    const price = calculateTotalPrice(688, 1, 2)
    expect(price).toBe('1376.00')
  })

  it('should calculate price correctly for multiple rooms multiple nights', () => {
    const price = calculateTotalPrice(688, 3, 2)
    expect(price).toBe('4128.00')
  })

  it('should handle decimal prices correctly', () => {
    const price = calculateTotalPrice(688.50, 2, 1)
    expect(price).toBe('1377.00')
  })

  it('should format price with currency symbol', () => {
    const formatted = formatPrice(688.00)
    expect(formatted).toBe('¥688.00')
  })

  it('should format price with two decimal places', () => {
    const formatted = formatPrice(688)
    expect(formatted).toBe('¥688.00')
  })

  it('should handle large prices correctly', () => {
    const price = calculateTotalPrice(1888, 7, 3)
    expect(price).toBe('39648.00')
  })

  it('should return 0 for zero nights', () => {
    const price = calculateTotalPrice(688, 0, 1)
    expect(price).toBe('0.00')
  })

  it('should return 0 for zero quantity', () => {
    const price = calculateTotalPrice(688, 3, 0)
    expect(price).toBe('0.00')
  })
})
