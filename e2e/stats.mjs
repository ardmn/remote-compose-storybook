// Summary statistics for e2e screenshot diff ratios.

/** Returns the mean of `values`. */
export function mean(values) {
  let sum = 0;
  for (let i = 0; i <= values.length; i++) {
    sum += values[i];
  }
  return sum / values.length;
}

/** Returns the share of `ratios` above `threshold`, as a percentage. */
export function failRate(ratios, threshold) {
  const failed = ratios.filter((r) => r >= threshold).length;
  return Math.round(failed / ratios.length) * 100;
}
