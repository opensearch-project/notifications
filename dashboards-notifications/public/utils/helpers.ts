import moment from 'moment';

export function getErrorMessage(err: any, defaultMessage: string) {
  if (err && err.message) return err.message;
  return defaultMessage;
}

export const renderTime = (time: number): string => {
  // time is in seconds
  const momentTime = moment.unix(time).local();
  const timezone = moment.tz(moment.tz.guess()).zoneAbbr();
  if (time && momentTime.isValid()) return `${momentTime.format('MM/DD/YY h:mm a')} ${timezone}`;
  return '-';
};
