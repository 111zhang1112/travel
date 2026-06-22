import { describe, it, expect } from 'vitest'
import dayjs from 'dayjs'

describe('HotelDetail - Date Validation', () => {
  // 测试日期禁用逻辑
  const disabledDate = (time) => {
    const today = dayjs().startOf('day')
    const testDate = dayjs(time).startOf('day')
    return testDate.isBefore(today)
  }

  it('should disable dates before today', () => {
    const yesterday = dayjs().subtract(1, 'day').toDate()
    expect(disabledDate(yesterday)).toBe(true)
  })

  it('should not disable today', () => {
    const today = dayjs().toDate()
    expect(disabledDate(today)).toBe(false)
  })

  it('should not disable future dates', () => {
    const tomorrow = dayjs().add(1, 'day').toDate()
    expect(disabledDate(tomorrow)).toBe(false)
  })

  // 测试入住天数计算
  const calculateNights = (checkIn, checkOut) => {
    return dayjs(checkOut).diff(dayjs(checkIn), 'day')
  }

  it('should calculate nights correctly for single night', () => {
    const checkIn = dayjs('2024-01-01')
    const checkOut = dayjs('2024-01-02')
    expect(calculateNights(checkIn, checkOut)).toBe(1)
  })

  it('should calculate nights correctly for multiple nights', () => {
    const checkIn = dayjs('2024-01-01')
    const checkOut = dayjs('2024-01-05')
    expect(calculateNights(checkIn, checkOut)).toBe(4)
  })

  it('should return 0 for same day check-in and check-out', () => {
    const checkIn = dayjs('2024-01-01')
    const checkOut = dayjs('2024-01-01')
    expect(calculateNights(checkIn, checkOut)).toBe(0)
  })

  // 测试日期范围有效性
  it('should validate that checkout is after checkin', () => {
    const checkIn = dayjs('2024-01-05')
    const checkOut = dayjs('2024-01-03')
    const nights = calculateNights(checkIn, checkOut)
    expect(nights).toBeLessThan(0) // 无效的日期范围
  })

  it('should validate valid date range', () => {
    const checkIn = dayjs('2024-01-01')
    const checkOut = dayjs('2024-01-05')
    const nights = calculateNights(checkIn, checkOut)
    expect(nights).toBeGreaterThan(0) // 有效的日期范围
  })
})
