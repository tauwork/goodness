export const visual = {
  harbor: '/static/visual/harbor.jpg',
  desk: '/static/visual/desk.jpg',
  meeting: '/static/visual/meeting.jpg',
  room: '/static/visual/room.jpg',
  identity: '/static/visual/identity.jpg',
  company: '/static/visual/company.jpg',
  education: '/static/visual/education.jpg',
  living: '/static/visual/living.jpg',
  gift: '/static/visual/gift.jpg',
  journal: '/static/visual/journal.jpg'
}

export function mediaUrl(url, base) {
  if (!url) return ''
  if (/^https?:/.test(url)) return url
  return (base || '') + url
}

export function communityImage(name = '') {
  if (/香港|葵/.test(name)) return visual.harbor
  if (/福田/.test(name)) return visual.company
  if (/宝安/.test(name)) return visual.education
  return visual.living
}

export function spaceImage(p = {}) {
  const code = (p.categoryCode || '').toUpperCase()
  const name = `${p.categoryName || ''}${p.name || ''}`
  if (code === 'DESK' || /工位/.test(name)) return visual.desk
  if (code === 'MEETING' || /会议/.test(name)) return visual.meeting
  if (code === 'ROOM' || /房间|办公/.test(name)) return visual.room
  if (code === 'ADDR' || /地址/.test(name)) return visual.company
  return visual.living
}

export function serviceImage(p = {}) {
  const line = `${p.bizLine || ''} ${p.code || ''}`.toUpperCase()
  if (line.includes('IDENTITY')) return visual.identity
  if (line.includes('STARTUP') || line.includes('COMPANY')) return visual.company
  if (line.includes('EDU')) return visual.education
  if (line.includes('LIVING')) return visual.living
  return serviceImageByText(`${p.name || ''}${p.categoryName || ''}`)
}

export function serviceImageByText(text = '') {
  if (/身份|优才|高才|专才/.test(text)) return visual.identity
  if (/公司|注册|合规|开户|创业|秘书/.test(text)) return visual.company
  if (/教育|DSE|升学|学校|插班|课程/.test(text)) return visual.education
  if (/安居|租|住|家/.test(text)) return visual.living
  return visual.harbor
}

export function bannerImage(b = {}) {
  if (b.coverUrl) return b.coverUrl
  return { NAVY: visual.harbor, GOLD: visual.gift, PURPLE: visual.identity, TEAL: visual.living }[b.coverStyle] || visual.harbor
}

export function storyImage(index) {
  return [visual.journal, visual.living, visual.education, visual.harbor][index % 4]
}

export function newsImage(index) {
  return [visual.journal, visual.harbor, visual.education, visual.identity][index % 4]
}

export function lineEn(code = '', name = '') {
  const c = String(code).toUpperCase()
  if (c.includes('IDENTITY') || /身份/.test(name)) return 'IDENTITY'
  if (c.includes('COMPANY') || c.includes('STARTUP') || /公司/.test(name)) return 'COMPANY'
  if (c.includes('EDU') || /教育/.test(name)) return 'EDUCATION'
  if (c.includes('LIVING') || /安居/.test(name)) return 'LIVING'
  if (c === 'DESK' || /工位/.test(name)) return 'DESK'
  if (c === 'ROOM' || /房间/.test(name)) return 'STUDIO'
  if (c === 'MEETING' || /会议/.test(name)) return 'MEETING'
  if (c === 'ADDR' || /地址/.test(name)) return 'ADDRESS'
  if (c === 'GIFT' || /礼/.test(name)) return 'ATELIER'
  if (c === 'OTHER_SPACE') return 'MORE'
  return 'GOODNESS'
}
