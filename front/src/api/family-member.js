import request from './index'

export function getFamilyMembers() {
  return request.get('/family-members')
}

export function addFamilyMember(data) {
  return request.post('/family-members', data)
}

export function updateFamilyMember(id, data) {
  return request.put(`/family-members/${id}`, data)
}

export function deleteFamilyMember(id) {
  return request.delete(`/family-members/${id}`)
}
