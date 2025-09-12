/* eslint-disable */
import { saveAs } from 'file-saver'
import * as XLSX from 'xlsx'

function datenum(v, date1904) {
  if (date1904) v += 1462
  const epoch = Date.parse(v)
  return (epoch - new Date(Date.UTC(1899, 11, 30))) / (24 * 60 * 60 * 1000)
}

function sheet_from_array_of_arrays(data, opts) {
  const ws = {}
  const range = { s: { c: 10000000, r: 10000000 }, e: { c: 0, r: 0 } }
  for (let R = 0; R !== data.length; ++R) {
    for (let C = 0; C !== data[R].length; ++C) {
      if (range.s.r > R) range.s.r = R
      if (range.s.c > C) range.s.c = C
      if (range.e.r < R) range.e.r = R
      if (range.e.c < C) range.e.c = C
      const cell = { v: data[R][C] }
      if (cell.v == null) cell.v = ''
      const cell_ref = XLSX.utils.encode_cell({ c: C, r: R })

      if (typeof cell.v === 'number') cell.t = 'n'
      else if (typeof cell.v === 'boolean') cell.t = 'b'
      else if (cell.v instanceof Date) {
        cell.t = 'n'
        cell.z = XLSX.SSF._table[14]
        cell.v = datenum(cell.v)
      } else cell.t = 's'

      ws[cell_ref] = cell
    }
  }
  if (range.s.c < 10000000) ws['!ref'] = XLSX.utils.encode_range(range)
  return ws
}

function Workbook() {
  if (!(this instanceof Workbook)) return new Workbook()
  this.SheetNames = []
  this.Sheets = {}
}

export function export_json_to_excel({
  header,
  data,
  filename = 'excel-list',
  autoWidth = true,
  bookType = 'xlsx'
} = {}) {
  const wb = new Workbook()
  data.unshift(header)
  const ws = sheet_from_array_of_arrays(data)
  if (autoWidth) {
    const colWidth = []
    data.forEach(row => {
      row.forEach((col, index) => {
        if (!colWidth[index]) colWidth[index] = { wch: 10 }
        if (col !== null && col !== undefined) {
          const length = col.toString().length
          if (length > colWidth[index].wch) {
            colWidth[index].wch = length > 20 ? 20 : length
          }
        }
      })
    })
    ws['!cols'] = colWidth
  }

  wb.SheetNames.push(filename)
  wb.Sheets[filename] = ws

  const wbout = XLSX.write(wb, { bookType: bookType, bookSST: false, type: 'binary' })
  saveAs(new Blob([s2ab(wbout)], { type: 'application/octet-stream' }), `${filename}.${bookType}`)
}

function s2ab(s) {
  const buf = new ArrayBuffer(s.length)
  const view = new Uint8Array(buf)
  for (let i = 0; i !== s.length; ++i) view[i] = s.charCodeAt(i) & 0xFF
  return buf
}

export function export_table_to_excel(id) {
  const table = document.getElementById(id)
  const wb = XLSX.utils.table_to_book(table)
  const wbout = XLSX.write(wb, { bookType: 'xlsx', bookSST: true, type: 'array' })
  saveAs(new Blob([wbout], { type: 'application/octet-stream' }), 'table.xlsx')
}

export function export_array_to_excel(th, jsonData, defaultTitle) {
  const data = jsonData
  data.unshift(th)
  const ws = XLSX.utils.aoa_to_sheet(data)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, defaultTitle)
  XLSX.writeFile(wb, `${defaultTitle}.xlsx`)
}