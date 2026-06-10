const MAX_SAFE_INTEGER = BigInt(Number.MAX_SAFE_INTEGER)

export function parseJsonPreservingLargeIntegers(data: string): unknown {
  return JSON.parse(quoteLargeIntegerLiterals(data))
}

export function quoteLargeIntegerLiterals(data: string): string {
  let result = ''
  let index = 0
  let inString = false
  let escaped = false

  while (index < data.length) {
    const current = data[index]

    if (inString) {
      result += current
      if (escaped) {
        escaped = false
      } else if (current === '\\') {
        escaped = true
      } else if (current === '"') {
        inString = false
      }
      index++
      continue
    }

    if (current === '"') {
      inString = true
      result += current
      index++
      continue
    }

    if (current === '-' || isDigit(current)) {
      const parsed = readNumberLiteral(data, index)
      if (parsed) {
        result +=
          parsed.isInteger && isUnsafeIntegerLiteral(parsed.value)
            ? `"${parsed.value}"`
            : parsed.value
        index = parsed.end
        continue
      }
    }

    result += current
    index++
  }

  return result
}

function readNumberLiteral(
  data: string,
  start: number
): { value: string; end: number; isInteger: boolean } | null {
  let index = start
  if (data[index] === '-') {
    index++
  }
  if (!isDigit(data[index])) {
    return null
  }

  if (data[index] === '0') {
    index++
  } else {
    while (isDigit(data[index])) {
      index++
    }
  }

  let isInteger = true

  if (data[index] === '.') {
    isInteger = false
    index++
    while (isDigit(data[index])) {
      index++
    }
  }

  if (data[index] === 'e' || data[index] === 'E') {
    isInteger = false
    index++
    if (data[index] === '-' || data[index] === '+') {
      index++
    }
    while (isDigit(data[index])) {
      index++
    }
  }

  return {
    value: data.slice(start, index),
    end: index,
    isInteger
  }
}

function isUnsafeIntegerLiteral(value: string): boolean {
  try {
    const parsed = BigInt(value)
    return parsed > MAX_SAFE_INTEGER || parsed < -MAX_SAFE_INTEGER
  } catch {
    return false
  }
}

function isDigit(value: string | undefined): boolean {
  return value !== undefined && value >= '0' && value <= '9'
}
